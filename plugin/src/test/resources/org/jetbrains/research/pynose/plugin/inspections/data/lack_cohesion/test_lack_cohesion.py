import unittest


class <warning descr="Test cases in this test suite are not cohesive">SomeClass</warning>(unittest.TestCase):

    def __init__(self):
        super().__init__()
        self.x = 'Hello'
        self.y = 1
        self.z = 5
        self.w = "hi"

    def test_inc_t(self):
        self.y += 1

    def test_dec_z(self):
        self.z -= 1

    def test_print_x(self):
        print(x)

    def test_check_hi(self):
        assert self.w == "hi"
