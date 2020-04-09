from distutils.core import setup
from Cython.Build import cythonize

setup(name='CACompute',
      ext_modules=cythonize("CACompute.pyx",
                            annotate=True,
                            compiler_directives={'language_level' : 3}))
