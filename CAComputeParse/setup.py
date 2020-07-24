from distutils.core import setup
from Cython.Build import cythonize

setup(name='CAComputeParse',
      ext_modules=cythonize("CAComputeParse.pyx",
                            annotate=True,
                            compiler_directives={'language_level' : 3,
                                                 'cdivision' : True}))
