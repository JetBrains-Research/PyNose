import unittest

class SomeClass(unittest.TestCase):
    def some_fun(self):
        pass

class <weak_warning descr="Consider changing the name of your test suite to a non-default one to better reflect its content">MyTestCase</weak_warning>(SomeClass):
    def test_something(self):
        pass
