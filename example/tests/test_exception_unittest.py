import unittest


class SomeClass(unittest.TestCase):

    def setUp(self):
        self.x = 10

    def test_division(self):
        with self.assertRaises(ZeroDivisionError):
            for i in range(5):
                print(self.x / i)
