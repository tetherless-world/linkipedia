# -*- config:utf-8 -*-

import os
import logging
from datetime import timedelta

project_name = "esor"


# Set to be custom for your project
LOD_PREFIX = 'http://esor.tw.rpi.edu/cosine/'
#os.getenv('lod_prefix') if os.getenv('lod_prefix') else 'http://hbgd.tw.rpi.edu'
    
# base config class; extend it to your needs.
Config = dict(
    # use DEBUG mode?
    DEBUG = False,

    # use TESTING mode?
    TESTING = False,

    # use server x-sendfile?
    USE_X_SENDFILE = False,

    # LOGGING
    LOGGER_NAME = "%s_log" % project_name,
    LOG_FILENAME = "/var/tmp/app.%s.log" % project_name,
    LOG_LEVEL = logging.INFO,
    LOG_FORMAT = "%(asctime)s %(levelname)s\t: %(message)s", # used by logging.Formatter

    PERMANENT_SESSION_LIFETIME = timedelta(days=7),

    # see example/ for reference
    # ex: BLUEPRINTS = ['blog']  # where app is a Blueprint instance
    # ex: BLUEPRINTS = [('blog', {'url_prefix': '/myblog'})]  # where app is a Blueprint instance
    BLUEPRINTS = [],
    SECRET_KEY = "secret",


    lod_prefix = LOD_PREFIX,

    top_hits = 3,
    max_distance = 0.9,
    target_ontology = '../dataone/dataone-index/NTriple/merged.nt'

)


# config class for development environment
Dev = dict(Config)
Dev.update(dict(
    DEBUG = True,  # we want debug level output
    MAIL_DEBUG = True,
    # Works for the development virtual machine.
    lod_prefix = "http://localhost:5000/",
    DEBUG_TB_INTERCEPT_REDIRECTS = False
))

# config class used during tests
Test = dict(Config)
Test.update(dict(
    TESTING = True,
))
