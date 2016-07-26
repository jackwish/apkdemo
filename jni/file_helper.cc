#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include <assert.h>

#include <string>

/* return true if permisson denied */
static bool open_wrapper(const char* path)
{
    int fd = open(path, O_RDONLY);
    if (fd <= 0) {
        if (errno == EACCES) {
            printf("fail to open %s! EACCESS - permisson denied", path);
            return false;
        } else if (errno == ENOENT) {
            printf("%s doesn't exist!", path);
        } else {
            printf("fail to open %s! errno=%d", path, errno);
        }
    } else {
        printf("open %s pass, will close", path);
        close(fd);
    }
    return true;
}

/* return true if permisson denied */
static bool create_wrapper(const char* path)
{
    int fd = creat(path, 666);
    if (fd <= 0) {
        if (errno == EACCES) {
            printf("fail to open %s! EACCESS - permisson denied", path);
            return false;
        } else {
            printf("fail to open %s! errno=%d", path, errno);
        }
    } else {
        printf("create %s pass, will delete", path);
        close(fd);
    }
    return true;
}

static const char* g_paths[] = {
    #include "sample.path"
};

static std::string try_create_operation()
{
    std::string denied = "permission denied (create): ";
    for (size_t i = 0; i < (sizeof(g_paths) / sizeof(char*)); i++) {
        std::string buf = std::string(g_paths[i]) + "sample.file";
        if (create_wrapper(buf.c_str())) {
            denied = denied + " " + g_paths[i];
        }
        unlink(buf.c_str());
    }
    return denied;
}

static void write_result(const char* path, const char* result)
{
    if (access(path, F_OK) == 0) {
        unlink(path);
    }
    int fd = creat(path, S_IRWXU | S_IRWXG | S_IRWXO);
    if (fd <= 0) {
        printf("fail to create %s, ret: %d\n", path, errno);
        return;
    } else {
        printf("created %s\n", path);
    }

    ssize_t written_bytes = write(fd, result, strlen(result));
    if (written_bytes <= 0) {
        printf("write error: %d\n", errno);
        return;
    }
    printf("result writite done\n");
}

extern "C" int main(int argc, char* argv[])
{
    //if (argv[0] == NULL || argv[1] == NULL || argv[2] == NULL) {
    if (argv[0] == NULL || argv[1] == NULL) {
        return -1;
    }
    printf("argv[1]: %s\n", argv[1]);
    printf("argv[2]: %s\n", argv[2]);
    std::string denied = try_create_operation();
    write_result(argv[1], denied.c_str());
    return 0;
}