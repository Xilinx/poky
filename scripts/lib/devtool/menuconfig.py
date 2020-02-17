# OpenEmbedded Development tool - menuconfig command plugin
#
# Copyright (C) 2018 Xilinx
# Written by: Chandana Kalluri <ckalluri@xilinx.com>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""Devtool menuconfig plugin"""

import os
import bb
import logging
import argparse
import re
import glob
from devtool import setup_tinfoil, parse_recipe, DevtoolError, standard, exec_build_env_command
from devtool import check_workspace_recipe
logger = logging.getLogger('devtool')

def menuconfig(args, config, basepath, workspace):
    """Entry point for the devtool 'menuconfig' subcommand"""

    rd = ""
    kconfigpath = ""
    localfilesdir = ""
    fragname = "devtool-fragment.cfg"

    tinfoil = setup_tinfoil(basepath=basepath)
    try:
        rd = parse_recipe(config, tinfoil, args.component, appends=True, filter_workspace=False)
        if not rd:
            return 1

        check_workspace_recipe(workspace, args.component)
        pn = rd.getVar('PN', True)

        if not rd.getVarFlag('do_menuconfig','task'):
            raise DevtoolError("This recipe does not support menuconfig option")

        srctree=rd.getVar('S',True)

        # add check to see if oe_local_files exists or not
        localfilesdir = os.path.join(srctree,'oe-local-files')
        if not os.path.exists(localfilesdir):
            bb.utils.mkdirhier(localfilesdir)
            # Add gitignore to ensure source tree is clean
            gitignorefile = os.path.join(localfilesdir,'.gitignore')
            with open(gitignorefile, 'w') as f:
                f.write('# Ignore local files, by default. Remove this file if you want to commit the directory to Git\n')
                f.write('*\n')

        if args.allow_append:
             with open(os.path.join(srctree,'.run-devtool-menuconfig'),'w') as f:
                     f.write('RUN-DEVTOOL-MENUCONFIG=1')


    finally:
        tinfoil.shutdown()

    if args.allow_append:
           if not os.path.exists(os.path.join(localfilesdir,'devtool-fragment.cfg')):
                   fragname = "devtool-fragment.cfg"
           else:
                   fragname = "devtool-fragment_tmp001.cfg"


    logger.info('Launching menuconfig')
    exec_build_env_command(config.init_path, basepath, 'bitbake -c menuconfig %s' % pn, watch=True)
    fragment = os.path.join(localfilesdir, fragname)

    res = standard._create_kconfig_diff(srctree,rd,fragment)

    return 0

def register_commands(subparsers, context):
    """register devtool subcommands from this plugin"""
    parser_menuconfig = subparsers.add_parser('menuconfig',help='Alter build-time configuration for a recipe', description='Launches the make menuconfig command (for recipes where do_menuconfig is available), allowing users to make changes to the build-time configuration. Creates a config fragment corresponding to changes made.', group='advanced')
    parser_menuconfig.add_argument('component', help='compenent to alter config')
    parser_menuconfig.add_argument('-a','--allow-append',action="store_true",help='append devtool-fragment.cfg to previous iteration fragment')
    parser_menuconfig.set_defaults(func=menuconfig,fixed_setup=context.fixed_setup)
