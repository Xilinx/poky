inherit kernel-uboot

python __anonymous () {
    if "uImage" in (d.getVar('KERNEL_IMAGETYPES') or "").split():
        depends = d.getVar("DEPENDS")
        depends = "%s u-boot-mkimage-native" % depends
        d.setVar("DEPENDS", depends)

        # Override KERNEL_IMAGETYPE_FOR_MAKE variable, which is internal
        # to kernel.bbclass . We override the variable here, since we need
        # to build uImage using the kernel build system if and only if
        # KEEPUIMAGE == yes. Otherwise, we pack compressed vmlinux into
        # the uImage .
        if d.getVar("KEEPUIMAGE") != 'yes':
            typeformake = d.getVar("KERNEL_IMAGETYPE_FOR_MAKE") or ""
            if "uImage" in typeformake.split():
                d.setVar('KERNEL_IMAGETYPE_FOR_MAKE', typeformake.replace('uImage', 'vmlinux'))

            # Enable building of uImage with mkimage
            bb.build.addtask('do_uboot_mkimage', 'do_install', 'do_kernel_link_images', d)

            if d.getVar('INITRAMFS_IMAGE_BUNDLE') == '1' :
                bb.build.addtask('do_uboot_mkimage_initramfs', 'do_deploy', 'do_bundle_initramfs', d)

}

do_uboot_mkimage[dirs] += "${B}"
do_uboot_mkimage() {

	local uimage_name="$1"
	[ -z "$uimage_name" ] && uimage_name="uImage"

	uboot_prep_kimage

	ENTRYPOINT=${UBOOT_ENTRYPOINT}
	if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
		ENTRYPOINT=`${HOST_PREFIX}nm ${B}/vmlinux | \
			awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
	fi

	uboot-mkimage -A ${UBOOT_ARCH} -O linux -T kernel -C "${linux_comp}" -a ${UBOOT_LOADADDRESS} -e $ENTRYPOINT -n "${DISTRO_NAME}/${PV}/${MACHINE}" -d linux.bin ${B}/arch/${ARCH}/boot/$uimage_name
	rm -f linux.bin
}

do_uboot_mkimage_initramfs() {
	cd ${B}
	do_uboot_mkimage uImage.initramfs
}
