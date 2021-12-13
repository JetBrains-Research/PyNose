import unittest

class SomeClass(unittest.TestCase):

    def __init__(self):
        super().__init__()
        self.x = 'Hello'
        self.y = 1

    def <weak_warning descr="Test case contains too many setup steps, consider moving them to a fixture or to a separate method">test_something</weak_warning>(self):
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


    def <weak_warning descr="Test case contains too many setup steps, consider moving them to a fixture or to a separate method">test_something_else</weak_warning>(self):
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

    def <weak_warning descr="Test case contains too many setup steps, consider moving them to a fixture or to a separate method">test_something</weak_warning>(self):
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
