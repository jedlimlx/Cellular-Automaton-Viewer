from setuptools.extension import Extension
from setuptools import setup
from Cython.Build import cythonize

ext_modules = cythonize("*/*.pyx",
                        compiler_directives={'language_level' : 3,
                                             'cdivision' : True})

setup(ext_modules=ext_modules)
