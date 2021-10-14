import unittest

class SomeClass(unittest.TestCase):
    def some_fun(self):
        pass

class <warning descr="Test smell: Default Test in class `MyTestCase`">MyTestCase</warning>(SomeClass):
    def test_something(self):
        pass
