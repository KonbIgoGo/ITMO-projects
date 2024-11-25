QT       += core gui
greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

CONFIG += c++17

# You can make your code fail to compile if it uses deprecated APIs.
# In order to do so, uncomment the following line.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

SOURCES += \
    cell.cpp \
    congrats.cpp \
    counter.cpp \
    field.cpp \
    main.cpp \
    mainwindow.cpp \
    settings.cpp \
    widget_align_wrapper.cpp

HEADERS += \
    cell.h \
    congrats.h \
    counter.h \
    field.h \
    mainwindow.h \
    settings.h \
    types.h \
    widget_align_wrapper.h

TRANSLATIONS += \
    locals/game_ar_TD.ts \
    locals/game_en_US.ts \
    locals/game_ru_RU.ts
CONFIG += lrelease
CONFIG += embed_translations

# Default rules for deployment.
qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target

RESOURCES += \
    icons.qrc
