import unittest


class SomeClass(unittest.TestCase):
    x : int
    s : str
    w = 2

    def setUp(self):
        self.x = 10
        self.s = "hello"

    def <weak_warning descr="Test suite fixture's setup method is not used in this test case">test_something</weak_warning>(self):
        print("Hello, world!")

    def <weak_warning descr="Test suite fixture's setup method is not used in this test case">test_something_else</weak_warning>(self):
        assert self.w != 1
