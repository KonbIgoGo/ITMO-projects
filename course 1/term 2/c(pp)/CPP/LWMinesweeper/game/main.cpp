#include "mainwindow.h"

#include <QApplication>
#include <QDebug>
#include <QLocale>
#include <QTranslator>

int main(int argc, char *argv[])
{
	QApplication app(argc, argv);
	Q_INIT_RESOURCE(icons);

	QApplication::setOrganizationName("minesweeper");
	QSettings::setDefaultFormat(QSettings::IniFormat);
	QSettings::setPath(QSettings::IniFormat, QSettings::UserScope, QCoreApplication::applicationDirPath());

	QTranslator translator;
	const QStringList uiLanguages = QLocale::system().uiLanguages();
	for (const QString &locale : uiLanguages)
	{
		const QString baseName = "game_" + QLocale(locale).name();
		if (translator.load(":/i18n/" + baseName))
		{
			app.installTranslator(&translator);
			break;
		}
	}

	bool dbg = false;
	if (argc > 1)
	{
		QString arg_val(argv[1]);
		dbg = arg_val == "1" || arg_val == "dbg";
	}

	MainWindow window(dbg);
	window.show();
	return app.exec();
}
