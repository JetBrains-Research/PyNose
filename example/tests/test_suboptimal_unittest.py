import unittest


class SomeClass(unittest.TestCase):
    X = 10
    Y = 20
    Z = {10, 20}

    def test_something(self):
        self.assertTrue(self.X != self.Y)
        self.assertTrue(self.X == self.Y)
        self.assertTrue(self.X >= self.Y)

    def do_something(self):
        self.assertTrue(self.X not in self.Y)

    def test_something_else(self):
        self.assertTrue(self.X not in self.Z)
        self.assertTrue(self.X in self.Z)
        self.assertTrue(self.X < self.Y)


class OtherClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something_other(self):
        self.assertFalse(self.X in self.Y)


class AnotherClass:
    X = True

    def test_something_another(self):
        self.assertIs(self.X, True)
