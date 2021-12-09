class TestClass:

    def __init__(self):
        super().__init__()
        self.x = 'Hello'
        self.y = 1
        self.z = 5
        self.w = "hi"

    def inc_variables(self):
        self.x = "Bye"
        self.y += 1
        self.z += 5

    def print_variables(self):
        print(self.x)
        print(self.w)
        print(self.y + self.z)

    def test_compare_variables(self):
        self.assertNotEqual(self.x, self.w)
        self.assertLess(self.y, self.z)
