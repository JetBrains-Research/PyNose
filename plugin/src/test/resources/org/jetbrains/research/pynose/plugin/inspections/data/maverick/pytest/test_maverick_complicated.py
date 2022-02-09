class TestClass():
    x : int
    s : str
    z : str
    w = 2

    @classmethod
    def setUpClass(cls):
        cls.x = 10
        cls.s = "hello"
        cls.z = "bye"

    def test_something(self):
        print(self.s + ", world!")
        print(self.z)

    def <weak_warning descr="Test suite fixture's setup method is not used in this test case">test_something_else</weak_warning>(self):
        assert self.w != 1
