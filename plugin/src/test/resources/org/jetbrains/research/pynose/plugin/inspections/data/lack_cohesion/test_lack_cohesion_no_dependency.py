import unittest


class SomeClass:

    def __init__(self):
        self.x = 'Hello'
        self.y = 1
        self.z = 5
        self.w = "hi"

    def test_inc_t(self):
        self.y += 1

    def test_dec_z(self):
        self.z -= 1

    def test_print_x(self):
        print(self.x)

    def test_check_hi(self):
        assert self.w == "hi"
