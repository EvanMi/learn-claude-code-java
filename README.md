# My Package

A sample Python package with utility functions.

## Installation

```bash
pip install -e .
```

## Usage

```python
from my_package import add_numbers, multiply_numbers, is_even, reverse_string, calculate_average

# Basic arithmetic
result = add_numbers(2, 3)  # 5
result = multiply_numbers(4, 5)  # 20

# Number utilities
is_even(4)  # True
is_even(7)  # False

# String utilities
reverse_string("hello")  # "olleh"

# Statistics
calculate_average([1, 2, 3, 4, 5])  # 3.0
```

## Available Functions

- `add_numbers(a, b)`: Add two numbers
- `multiply_numbers(a, b)`: Multiply two numbers
- `is_even(number)`: Check if a number is even
- `reverse_string(text)`: Reverse a string
- `calculate_average(numbers)`: Calculate average of a list of numbers
- `factorial(n)`: Calculate factorial of a non-negative integer
- `fibonacci(n)`: Generate Fibonacci sequence up to n terms

## Testing

Run tests with pytest:

```bash
pytest tests/
```

Or run tests with coverage:

```bash
pytest --cov=my_package tests/
```

## License

MIT