#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/fb.h>

int main() {
    int fbfd = 0;
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;

    // Open the framebuffer device
    fbfd = open("/dev/fb0", O_RDWR);
    if (fbfd == -1) {
        perror("Error: cannot open framebuffer device");
        return 1;
    }
    printf("Framebuffer device opened successfully\n");

    // Get variable screen information
    if (ioctl(fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
        perror("Error reading variable information");
        close(fbfd);
        return 1;
    }

    printf("Framebuffer resolution: %dx%d\n", vinfo.xres, vinfo.yres);
    printf("Bits per pixel: %d\n", vinfo.bits_per_pixel);

    // Get fixed screen information
    if (ioctl(fbfd, FBIOGET_FSCREENINFO, &finfo)) {
        perror("Error reading fixed information");
        close(fbfd);
        return 1;
    }

    printf("Framebuffer memory: %d bytes\n", finfo.smem_len);
    printf("Line length: %d bytes\n", finfo.line_length);

    close(fbfd);
    return 0;
}
