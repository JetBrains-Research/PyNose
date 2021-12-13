class <weak_warning descr="This test suite fixture's setup method is not used in some of the test cases">TestClass</weak_warning>:
    x : int
    s : str
    w = 2

    def setUp(self):
        self.x = 10
        self.s = "hello"

    def test_something(self):
        print("Hello, world!")

    def test_something_else(self):
        assert self.w != 1
