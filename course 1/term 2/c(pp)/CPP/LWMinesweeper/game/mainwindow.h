#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "congrats.h"
#include "field.h"
#include "settings.h"
#include "widget_align_wrapper.h"

#include <QApplication>
#include <QComboBox>
#include <QMainWindow>
#include <QPushButton>
#include <QTranslator>

class MainWindow : public QMainWindow
{
	Q_OBJECT
	QTranslator m_translator;
	bool m_dbg;

	const int m_MAX_S = 50;
	const int m_MAX_M = 2499;

	QString m_settings_file;

	QSettings m_settings;
	specs m_field_data = { 10, 10, 10 };
	Field *m_field = nullptr;
	WidgetAlignWrapper *m_central_widget = nullptr;
	QToolBar *m_tools;

	Congrats *m_win_window = new Congrats(QApplication::translate("Game", "YOU WIN"), this);
	Congrats *m_defeat_window = new Congrats(QApplication::translate("Game", "YOU LOSE"), this);
	Settings *m_settings_window = new Settings(this);
	QPushButton *m_settings_btn;
	QComboBox *m_language_list;

	void initGUI();
	void changeLanguage();
	unsigned int settingsGuard(QString, int, unsigned int);

  protected:
	void resizeEvent(QResizeEvent *) override;
	void changeEvent(QEvent *) override;

  public:
	MainWindow(bool dbg = false);
	~MainWindow();

  public slots:
	void loadSettings();
	void restart();
};
#endif	  // MAINWINDOW_H
