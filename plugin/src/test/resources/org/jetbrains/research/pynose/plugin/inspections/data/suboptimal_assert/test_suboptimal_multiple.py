import unittest

class SomeClass(unittest.TestCase):
    def test_something(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(X != Y)</warning>

    def do_something(self):
        self.assertTrue(X not in Y)

    def test_something_else(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(X not in Y)</warning>

class OtherClass(unittest.TestCase):
    def test_something_other(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertFalse(X in Y)</warning>

class AnotherClass():
    def test_something_another(self):
        assertIs(X, True)