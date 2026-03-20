package com.yumi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TaskManager {
    private static final List<String> statusList = List.of("pending", "in_progress", "completed");
    private final Path tasksDir;
    private int nextId;
    private final ObjectMapper objectMapper;

    public TaskManager(Path tasksDir) {
        this.tasksDir = tasksDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(tasksDir);
            this.nextId = maxId() + 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int maxId() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tasksDir, "task_*.json")) {
            List<Integer> ids = new ArrayList<>();
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                // Remove "task_" and ".json"
                String idStr = fileName.substring(5, fileName.length() - 5);
                try {
                    ids.add(Integer.parseInt(idStr));
                } catch (NumberFormatException e) {
                    // Skip invalid file names
                }
            }
            return ids.isEmpty() ? 0 : Collections.max(ids);
        }
    }

    private Task load(int taskId) throws IOException {
        Path path = tasksDir.resolve("task_" + taskId + ".json");
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Task " + taskId + " not found");
        }
        return objectMapper.readValue(path.toFile(), Task.class);
    }

    private void save(Task task) throws IOException {
        Path path = tasksDir.resolve("task_" + task.getId() + ".json");
        objectMapper.writeValue(path.toFile(), task);
    }

    public record CreateTaskCommand(String subject, String description) {}

    public String create(CreateTaskCommand command) {
        try {
            Task task = new Task(nextId, command.subject(), null == command.description() ? "" : command.description());
            save(task);
            nextId++;
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record GetTaskCommand(int taskId) {}

    public String get(GetTaskCommand command) {
        try {
            Task task = load(command.taskId());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record UpdateTaskCommand(int taskId, String status, List<Integer> addBlockedBy, List<Integer> addBlocks) {}

    public String update(UpdateTaskCommand command) {
        try {
            Task task = load(command.taskId());

            if (command.status() != null) {
                if (!statusList.contains(command.status())) {
                    throw new IllegalArgumentException("Invalid status: " + command.status());
                }
                task.setStatus(command.status());

                // When a task is completed, remove it from all other tasks' blockedBy
                if ("completed".equals(command.status())) {
                    clearDependency(command.taskId());
                }
            }

            if (command.addBlockedBy() != null && !command.addBlockedBy().isEmpty()) {
                Set<Integer> currentBlockedBy = new HashSet<>(task.getBlockedBy());
                currentBlockedBy.addAll(command.addBlockedBy());
                task.setBlockedBy(new ArrayList<>(currentBlockedBy));
            }

            if (command.addBlocks() != null && !command.addBlocks().isEmpty()) {
                Set<Integer> currentBlocks = new HashSet<>(task.getBlocks());
                currentBlocks.addAll(command.addBlocks());
                task.setBlocks(new ArrayList<>(currentBlocks));

                // Bidirectional: also update the blocked tasks' blockedBy lists
                for (Integer blockedId : command.addBlocks()) {
                    try {
                        Task blockedTask = load(blockedId);
                        if (!blockedTask.getBlockedBy().contains(command.taskId())) {
                            blockedTask.getBlockedBy().add(command.taskId());
                            save(blockedTask);
                        }
                    } catch (IllegalArgumentException e) {
                        // Blocked task doesn't exist, skip
                    }
                }
            }

            save(task);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(task);
        } catch (Exception e) {
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
        }
    }

    private void clearDependency(int completedId) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tasksDir, "task_*.json")) {
            for (Path file : stream) {
                Task task = objectMapper.readValue(file.toFile(), Task.class);
                if (task.getBlockedBy().contains(completedId)) {
                    task.getBlockedBy().remove(Integer.valueOf(completedId));
                    save(task);
                }
            }
        }
    }

    public String listAll() {
        try {
            List<Task> tasks = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(tasksDir, "task_*.json")) {
                List<Path> sortedFiles = new ArrayList<>();
                for (Path file : stream) {
                    sortedFiles.add(file);
                }
                sortedFiles.sort(Comparator.comparing(Path::getFileName));

                for (Path file : sortedFiles) {
                    tasks.add(objectMapper.readValue(file.toFile(), Task.class));
                }
            }

            if (tasks.isEmpty()) {
                return "No tasks.";
            }

            List<String> lines = getLines(tasks);

            return String.join("\n", lines);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getLines(List<Task> tasks) {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            String marker = switch (task.getStatus()) {
                case "pending" -> "[ ]";
                case "in_progress" -> "[>]";
                case "completed" -> "[x]";
                default -> "[?]";
            };

            String blocked = task.getBlockedBy().isEmpty() ? "" :
                " (blocked by: " + task.getBlockedBy() + ")";
            lines.add(marker + " #" + task.getId() + ": " + task.getSubject() + blocked);
        }
        return lines;
    }
}