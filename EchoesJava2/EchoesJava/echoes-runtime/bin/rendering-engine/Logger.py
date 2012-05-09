import Ice


def trace(type, msg):
    if Ice.Application.communicator():
        Ice.Application.communicator().getLogger().trace(type, msg)
    else:
        print type + ":" + msg

def warning(msg):
    if Ice.Application.communicator():
        Ice.Application.communicator().getLogger().warning(msg)
    else:
        print msg
    