import unittest


class SomeClass(unittest.TestCase):
    x : int
    s : str
    w = 2

    def setUp(self):
        self.x = 10
        self.s = "hello"

    def <weak_warning descr="This test suite fixture's setup method is not used in some of the test cases">test_something</weak_warning>(self):
        print("Hello, world!")

    def <weak_warning descr="This test suite fixture's setup method is not used in some of the test cases">test_something_else</weak_warning>(self):
        assert self.w != 1
