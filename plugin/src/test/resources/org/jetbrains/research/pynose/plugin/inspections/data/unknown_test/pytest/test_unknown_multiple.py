class TestClass:

    def <warning descr="This test case has no assertion in it">test_something</warning>(self):
        pass

    def do_something(self):
        pass

    def <warning descr="This test case has no assertion in it">test_something_else</warning>(self):
        x = 2


def <warning descr="This test case has no assertion in it">test_something_other</warning>(self):
    print("Hello")


def do_something(self):
    pass


class AnotherClass:

    def test_something_another(self):
        x = 3 + 6
