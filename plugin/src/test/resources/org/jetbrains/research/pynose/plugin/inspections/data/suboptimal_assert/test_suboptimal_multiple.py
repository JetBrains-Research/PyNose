import unittest

class SomeClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(self.X != self.Y)</warning>

    def do_something(self):
        self.assertTrue(self.X not in self.Y)

    def test_something_else(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertTrue(self.X not in self.Y)</warning>

class OtherClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something_other(self):
        <warning descr="The assertion is ambiguous and can be replaced by a more specific assertion">self.assertFalse(self.X in self.Y)</warning>

class AnotherClass:
    X = True

    def test_something_another(self):
        self.assertIs(self.X, True)