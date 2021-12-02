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
        self.assertTrue(self.X is not self.Z)
        self.assertTrue(self.X is self.Z)
        self.assertTrue(self.X)


class OtherClass(unittest.TestCase):
    X = 10
    Y = 20

    def test_something(self):
        self.assertIs(self.X, True)
        self.assertIsNot(self.X, False)
        self.assertIsNot(self.X, None)

    def test_something_other(self):
        self.assertNotEqual(self.X, False)
        self.assertFalse(self.X in self.Y)
        z = {10, 20}
        self.assertTrue(self.X not in z)
        self.assertFalse(self.X in z)


class AnotherClass:
    X = True

    def test_something_another(self):
        self.assertIs(self.X, True)
