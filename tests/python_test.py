# file: script1.py

import math
import time

def greet():
    print("Hello, welcome to the system!")
    print("Enjoy your stay.")

def calculate_area(radius):
    return math.pi * radius ** 2

def print_employee_names(names):
    for name in names:
        print(f"Employee: {name}")

def greet():
    print("Hello, welcome to the system!")
    print("Enjoy your stay.")

def factorial(n):
    if n <= 1:
        return 1
    else:
        return n * factorial(n - 1)

def sleep_and_print(seconds):
    time.sleep(seconds)
    print(f"Slept for {seconds} seconds")

def print_employee_names(names):
    for name in names:
        print(f"Employee: {name}")

def unique_function_1():
    print("This function does something unique in script1.")

def unique_function_2():
    print("Another unique feature here.")

def factorial_iterative(n):
    result = 1
    for i in range(2, n+1):
        result *= i
    return result

# Lots of filler code below to reach ~200 lines

def filler_func(i):
    print(f"Filler function number {i}")

for i in range(50):
    filler_func(i)

def nested_example(x):
    if x > 10:
        for i in range(x):
            print(i)
    else:
        print("Less than or equal to 10")

class Employee:
    def __init__(self, name):
        self.name = name

    def greet(self):
        print(f"Hello, I am {self.name}")

class Manager(Employee):
    def greet(self):
        super().greet()
        print("I manage people.")

# Duplicate method in Manager with slight variation
class Manager(Employee):
    def greet(self):
        super().greet()
        print("I manage people.")

def recursive_sum(n):
    if n == 0:
        return 0
    return n + recursive_sum(n - 1)

def unique_function_3():
    print("Unique code snippet in script1")

# More filler code

def filler_func_2(i):
    print(f"Another filler function {i}")

for i in range(50, 100):
    filler_func_2(i)

def print_welcome():
    print("Welcome to the Python script!")

print_welcome()

# file: script2.py

import math
import time

def greet():
    print("Hello, welcome to the system!")
    print("Enjoy your stay.")

def calculate_area(radius):
    return math.pi * radius ** 2

def print_employee_list(names):
    for name in names:
        print(f"Employee: {name}")

def factorial(n):
    if n <= 1:
        return 1
    else:
        return n * factorial(n - 1)

def sleep_and_report(seconds):
    time.sleep(seconds)
    print(f"Slept for {seconds} seconds")

def unique_function_a():
    print("Unique function in script2.")

def factorial_iter(n):
    result = 1
    for i in range(2, n+1):
        result *= i
    return result

def print_employee_list(names):
    for name in names:
        print(f"Employee: {name}")

def unique_function_b():
    print("Another unique function in script2.")

def filler_func(i):
    print(f"Filler function number {i}")

for i in range(60):
    filler_func(i)

def nested_check(x):
    if x > 10:
        for i in range(x):
            print(i)
    else:
        print("Less than or equal to 10")

class Employee:
    def __init__(self, name):
        self.name = name

    def greet(self):
        print(f"Hello, I am {self.name}")

class Manager(Employee):
    def greet(self):
        super().greet()
        print("I manage people.")

def recursive_sum(n):
    if n == 0:
        return 0
    return n + recursive_sum(n - 1)

def unique_function_c():
    print("Unique snippet in script2")

def filler_func_2(i):
    print(f"Another filler function {i}")

for i in range(60, 120):
    filler_func_2(i)

def print_welcome():
    print("Welcome to the Python script!")

print_welcome()
