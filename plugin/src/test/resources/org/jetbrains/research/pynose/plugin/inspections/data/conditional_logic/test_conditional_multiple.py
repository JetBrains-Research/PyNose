import unittest

class SomeClass(unittest.TestCase):

    def test_something(self):
        y = 10
        <warning descr="Consider removing conditions from tests to ensure that all statements are executed">if</warning> (y > 1):
            <warning descr="Consider removing conditions from tests to ensure that all statements are executed">for</warning> x in range(1, 20):
                y += 2

    def test_something_else(self):
        y = 20
        <warning descr="Consider removing conditions from tests to ensure that all statements are executed">while</warning>(y > 10):
            y -= 3
        s =<warning descr="Consider removing conditions from tests to ensure that all statements are executed">{num: num**2 for num in range(1, 11)}</warning>

    def not_a_test(self):
        if (2 > 1):
            for x in range(20):
                pass
        s = list(num**2 for num in range(1, 11))

class OtherClass(unittest.TestCase):

    def test_something(self):
        x = 10
        <warning descr="Consider removing conditions from tests to ensure that all statements are executed">if</warning> (x > 1):
            <warning descr="Consider removing conditions from tests to ensure that all statements are executed">for</warning> u in range(1, 20):
                x += 2

    def not_a_test(self):
        if (4 > 1):
            for i in range(20):
                pass
        d = list(num**2 for num in range(1, 11))