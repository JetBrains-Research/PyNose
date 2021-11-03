import unittest

class SomeClass(unittest.TestCase):
    def test_something(self):
        <warning descr="This statement is unnecessary as it's result will never change">assert 1 == 1</warning>

    def do_something(self):
        assert 2 < 2

    def test_something_else(self):
        <warning descr="This statement is unnecessary as it's result will never change">assert "a" < "a"</warning>

class OtherClass(unittest.TestCase):
    def test_something_other(self):
        self.assertTrue(4 >= 4)

class AnotherClass():
    def test_something_another(self):
        assert 5 == 5