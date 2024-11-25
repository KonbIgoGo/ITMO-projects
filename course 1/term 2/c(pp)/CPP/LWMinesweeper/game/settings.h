#ifndef SETTINGS_H
#define SETTINGS_H
#include "types.h"

#include <QDialog>
#include <QLineEdit>
#include <QPushButton>
#include <QRadioButton>
#include <QSettings>
class QRadioButton;

class Settings : public QDialog
{
	Q_OBJECT
	QString m_settings_file;

	QLineEdit *m_height, *m_width, *m_mines_amount;
	QRadioButton *m_rEasy, *m_rMedium, *m_rHard, *m_rCustom;
	QPushButton *m_ok_btn;
	QSettings m_settings;

	specs m_easy_preset = { 10, 10, 10 };
	specs m_medium_preset = { 30, 30, 300 };
	specs m_hard_preset = { 40, 40, 900 };

	void initGUI();
	void loadSettings();
	QString settingsGuard(QString, int, QString);
	bool btnGuard(QString);

	void setDefaulBtnState();
	int saveGuard(int val, int max_val);

  public:
	Settings(QWidget *parent = nullptr);

  private slots:
	void makeLinesInactive();
	void makeLinesActive();
	void setPreset();
	void saveSettings();
};

#endif	  // SETTINGS_H
