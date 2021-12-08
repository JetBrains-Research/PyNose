import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        with self.assertRaises(ValueError):
            x = 3
        with self.assertRaises(ValueError):
            x = 3
        try:
            x = 3
        except ValueError:
            print("Try again...")

    def do_something(self):
        raise NameError('HiThere')

    def test_something_else(self):
        raise NameError('HiThere')


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        try:
            x = 5
        except ValueError:
            print("Fail!")


class AnotherClass:

    def test_something_another(self):
        raise NameError('HiThere')
