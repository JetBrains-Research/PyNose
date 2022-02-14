import unittest


class Point(object):
    def __init__(self, x=0, y=0):
        self.x = x
        self.y = y

    def __str__(self):
        return '({} {})'.format(self.x, self.y)

    def __eq__(self, other):
        return self.x == other.x and self.y == other.y

    def __lt__(self, other):
        """ Меньше та точка, у которой меньше х. При одинаковых x, та, у которой меньше y."""
        if self.x == other.x:
            return self.y < other.y
        return self.x < other.x


class SomeClass(unittest.TestCase):
    def test(self):
        p0 = Point(3, 5)
        p1 = Point(3, 5)
        p2 = Point(-1, 7)
        p3 = Point(3, 1.17)
        # True
        assert(p0 == p1)
        self.assertEqual(p0, p1)
        # False
        assert((not p1 == p1))
        # True
        assert p0 != p1
        # True
        assert(p1 != p2)
