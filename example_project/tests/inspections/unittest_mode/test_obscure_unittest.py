import unittest


class SomeClass(unittest.TestCase):

    def __init__(self):
        super().__init__()
        self.x = 'Hello'
        self.y = 1

    def test_something(self):
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
        k = 11
        self.y += 1
        assert self.y == 2

    def test_something_else(self):
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
        k = 11
        assert a != b

    def do_something(self):
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
        k = 11
        self.y += 1
        assert self.y == 3


class OtherClass(unittest.TestCase):

    def test_something(self):
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
        k = 11
        assert a != b


class AnotherClass:

    def test_something(self):
        a = 1
        b = 2
        c = 3
        d = 4
        e = 5
        f = 6
        g = 7
        h = 8
        i = 9
        j = 10
        k = 11
        assert a != b
