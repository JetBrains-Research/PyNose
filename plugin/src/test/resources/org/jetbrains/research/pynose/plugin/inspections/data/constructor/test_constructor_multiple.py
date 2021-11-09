import unittest


class SomeClass(unittest.TestCase):

    def <warning descr="You can use the setUp() method to create the test fixture, instead of initializing the constructor">__init__</warning>(self):
        pass

    def test_something(self):
        pass


class OtherClass(unittest.TestCase):

    def <warning descr="You can use the setUp() method to create the test fixture, instead of initializing the constructor">__init__</warning>(self):
        pass

    def test_something_other(self):
        pass


class AnotherClass:

    def __init__(self):
        pass

    def test_something_else(self):
        pass
