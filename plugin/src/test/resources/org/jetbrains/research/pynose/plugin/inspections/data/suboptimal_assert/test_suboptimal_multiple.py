import unittest

class SomeClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(X != Y)</warning>

    def do_something(self):
        self.assertTrue(X not in Y)

    def test_something_else(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(X not in Y)</warning>

class OtherClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something_other(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertFalse(X in Y)</warning>

class AnotherClass:
    X = True

    def test_something_another(self):
        assertIs(X, True)