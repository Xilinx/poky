SUMMARY = "Provides support for the Tag Image File Format (TIFF)"
DESCRIPTION = "Library provides support for the Tag Image File Format \
(TIFF), a widely used format for storing image data.  This library \
provide means to easily access and create TIFF image files."
HOMEPAGE = "http://www.libtiff.org/"
LICENSE = "BSD-2-Clause"
LIC_FILES_CHKSUM = "file://COPYRIGHT;md5=34da3db46fab7501992f9615d7e158cf"

CVE_PRODUCT = "libtiff"

SRC_URI = "http://download.osgeo.org/libtiff/tiff-${PV}.tar.gz \
           file://0001-fix-the-FPE-in-tiffcrop-415-427-and-428.patch \
           file://CVE-2022-34526.patch \
           file://CVE-2022-2953.patch \
           file://CVE-2022-3970.patch \
           file://0001-Revised-handling-of-TIFFTAG_INKNAMES-and-related-TIF.patch \
           file://0001-tiffcrop-S-option-Make-decision-simpler.patch \
           file://0001-tiffcrop-disable-incompatibility-of-Z-X-Y-z-options-.patch \
           file://0001-tiffcrop-subroutines-require-a-larger-buffer-fixes-2.patch \
           file://CVE-2022-48281.patch \
           file://CVE-2023-0800_0801_0802_0803_0804.patch \
           file://CVE-2023-0795_0796_0797_0798_0799.patch \
           "

SRC_URI[sha256sum] = "917223b37538959aca3b790d2d73aa6e626b688e02dcda272aec24c2f498abed"

# exclude betas
UPSTREAM_CHECK_REGEX = "tiff-(?P<pver>\d+(\.\d+)+).tar"

# Tested with check from https://security-tracker.debian.org/tracker/CVE-2015-7313
# and 4.3.0 doesn't have the issue
CVE_CHECK_IGNORE += "CVE-2015-7313"
# These issues only affect libtiff post-4.3.0 but before 4.4.0,
# caused by 3079627e and fixed by b4e79bfa.
CVE_CHECK_IGNORE += "CVE-2022-1622 CVE-2022-1623"
# Issue is in jbig which we don't enable
CVE_CHECK_IGNORE += "CVE-2022-1210"

inherit autotools multilib_header

CACHED_CONFIGUREVARS = "ax_cv_check_gl_libgl=no"

PACKAGECONFIG ?= "cxx jpeg zlib lzma \
                  strip-chopping extrasample-as-alpha check-ycbcr-subsampling"

PACKAGECONFIG[cxx] = "--enable-cxx,--disable-cxx,,"
PACKAGECONFIG[jbig] = "--enable-jbig,--disable-jbig,jbig,"
PACKAGECONFIG[jpeg] = "--enable-jpeg,--disable-jpeg,jpeg,"
PACKAGECONFIG[zlib] = "--enable-zlib,--disable-zlib,zlib,"
PACKAGECONFIG[lzma] = "--enable-lzma,--disable-lzma,xz,"
PACKAGECONFIG[webp] = "--enable-webp,--disable-webp,libwebp,"

# Convert single-strip uncompressed images to multiple strips of specified
# size (default: 8192) to reduce memory usage
PACKAGECONFIG[strip-chopping] = "--enable-strip-chopping,--disable-strip-chopping,,"

# Treat a fourth sample with no EXTRASAMPLE_ value as being ASSOCALPHA
PACKAGECONFIG[extrasample-as-alpha] = "--enable-extrasample-as-alpha,--disable-extrasample-as-alpha,,"

# Control picking up YCbCr subsample info. Disable to support files lacking
# the tag
PACKAGECONFIG[check-ycbcr-subsampling] = "--enable-check-ycbcr-subsampling,--disable-check-ycbcr-subsampling,,"

# Support a mechanism allowing reading large strips (usually one strip files)
# in chunks when using TIFFReadScanline. Experimental 4.0+ feature
PACKAGECONFIG[chunky-strip-read] = "--enable-chunky-strip-read,--disable-chunky-strip-read,,"

PACKAGES =+ "tiffxx tiff-utils"
FILES:tiffxx = "${libdir}/libtiffxx.so.*"
FILES:tiff-utils = "${bindir}/*"

do_install:append() {
    oe_multilib_header tiffconf.h
}

BBCLASSEXTEND = "native nativesdk"
