import time
import unittest


def test_outside_of_test_classes():
    x = 1
    y = 2
    assert x != y


class NotTestCase:

    def __init__(self):
        self.a = 5


class SomeClass(unittest.TestCase):

    def __init__(self, *args, **kwargs):
        super(TestingClass, self).__init__(*args, **kwargs)
        self.x = 'Hello'
        self.y = 1
        self.z = 5
        self.w = "hi"

    def test_without_assertion(self):
        self.y += 1

    def test_print(self):
        print(self.x)

    def test_string_comparison(self):
        assert self.w == "hi"

    def test_unknown(self):
        x = 1

    def test_conditional(self):
        if 1 == 1:
            print("hello, world!")

        for i in range(1, 10):
            print(i)


class MyTestCase(SomeClass):
    def do_something(self):
        assert 1 == 1

    def test_something(self):
        assert 2 == 2


@unittest.skip("skip class")
class SkippedClass(unittest.TestCase):

    @unittest.skip("skip method")
    def test_obscure(self):
        x1 = 5
        x2 = 5
        x3 = 5
        x4 = 5
        x5 = 5
        x6 = 5
        x7 = 5
        x8 = 5
        x9 = 5
        x10 = 5
        x11 = 5
        x12 = 5
        x13 = 5
        y = 6
        self.assertTrue(1 < 2)
        self.assertTrue(x1 in y)
        self.assertFalse(x2 != y)
        self.assertIsNot(x3, False)
        self.assertTrue(x4 is not y)

    def test_roulette_1(self):
        self.assertEqual(4 + 5, 9)
        self.assertEqual(4 + 5, 3)
        self.assertEqual(4 + 5, 4)
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        self.assertEqual(1, 1)
        self.assertTrue(True)
        assert "H" == "J"

    def test_roulette(self):
        assert 1 == 1
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        assert True
        assert "H" == "J"
        assert "h" == "J"
        assert "K" == 2
        assert 2 == 4
        assert True
        assert 1 != 1
        assert False
        assert True

    def test_empty(self):
        pass

    def test_exception(self):
        try:
            f = open('myfile.txt')
            s = f.readline()
            i = int(s.strip())
        except OSError as err:
            print("OS error: {0}".format(err))

    def test_sleepy(self):
        time.sleep(5)
