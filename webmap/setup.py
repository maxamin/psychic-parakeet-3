from setuptools import setup

setup(
    name='webmap',
    version='1.0.0',
    packages=['bin', 'webmapCore', 'webmapCore.net', 'webmapCore.net.jsparser', 'webmapCore.file', 'webmapCore.attack',
              'webmapCore.config', 'webmapCore.config.attacks', 'webmapCore.config.reports',
              'webmapCore.config.vulnerabilities', 'webmapCore.report', 'webmapCore.language'],
    url='http://github.com/cehkunal/webmap',
    license='',
    author='Kunal Pachauri',
    author_email='cehkunal@gmail.com',
    description='Web Application Vulnerability Scanner'
)
