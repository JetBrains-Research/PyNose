import sys
import time
import pytest


def test_basic():
    x = 1
    y = 2
    assert x != y


@pytest.mark.skip(reason="no way of currently testing this")
def skipped_test():
    assert 1 == 1


@pytest.mark.xfail
def test_fail(self):
    assert 2 == 3


class SomeClass:

    def __init__(self):
        super().__init__()
        self.x = 'Hello'
        self.y = 1
        self.z = 5
        self.w = "hi"

    @pytest.mark.skipif(sys.version_info < (3, 7), reason="requires python3.7 or higher")
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


class OtherClass:

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
        assert 1 < 2
        assert x3 is not False
        assert x4 is not y

    @pytest.mark.skip(reason="no way of currently testing this")
    def test_skipped_roulette(self):
        assert 4 + 5 == 9
        assert 4 + 5 != 3
        assert 2 == 2
        assert 3 == 3
        assert 2 == 2
        assert True is True
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
        a = 1

    def test_pytest_exception(self):
        with pytest.raises(ZeroDivisionError):
            assert 1 / 0 > 0

    def test_sleepy(self):
        time.sleep(5)
