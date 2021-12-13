import unittest
import time


class SomeClass(unittest.TestCase):

    def test_something(self):
        <weak_warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</weak_warning>

    def do_something(self):
        time.sleep(5)

    def test_something_else(self):
        <weak_warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</weak_warning>


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        <weak_warning descr="Causing a thread to sleep can lead to different results on different devices, consider removing or documenting it">time.sleep(5)</weak_warning>


class AnotherClass:

    def test_something_another(self):
        time.sleep(5)
