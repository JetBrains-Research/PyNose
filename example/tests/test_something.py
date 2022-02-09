def test_something():
    try:
        for i in range(5):
            assert i / 10 > 0
    except ValueError:
        print("Try again")



