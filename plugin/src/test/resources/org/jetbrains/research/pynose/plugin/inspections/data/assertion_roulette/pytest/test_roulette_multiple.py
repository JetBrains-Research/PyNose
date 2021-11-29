class TestClass:

    def <warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_roulette</warning>(self):
        assert 2 == 2
        assert 4 + 5 == 9
        assert "H" == "J"

    def test_roulette_with_comments(self):
        assert 2 == 2, "comment"
        assert 4 + 5 == 9, "comment"
        assert "H" == "J", "comment"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False


def <warning descr="Test case has multiple non-documented assertions, consider separating or documenting them">test_roulette_outside</warning>(self):
    assert 4 + 5 == 9
    assert not 4 + 5 == 4
    assert 2 == 2
    assert "H" == "J"

def test_roulette_with_comments(self):
    assert 2 == 2, "comment"
    assert 4 + 5 == 9
    assert "H" == "J", "comment"

def do_something(self):
    assert True
    assert 1 == 1
    assert False


class OtherClass:

    def test_roulette(self):
        assert True
        assert 2 == 2
        assert "H" != "J"

    def do_something(self):
        assert True
        assert 1 == 1
        assert False
