#include "settings.h"

#include <QApplication>
#include <QGridLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QVBoxLayout>

Settings::Settings(QWidget *parent) : QDialog(parent)
{
	initGUI();
	m_settings_file = "./settings.ini";

	connect(m_rEasy, &QRadioButton::toggled, this, &Settings::setPreset);
	connect(m_rMedium, &QRadioButton::toggled, this, &Settings::setPreset);
	connect(m_rHard, &QRadioButton::toggled, this, &Settings::setPreset);

	connect(m_rEasy, &QRadioButton::toggled, this, &Settings::makeLinesInactive);
	connect(m_rMedium, &QRadioButton::toggled, this, &Settings::makeLinesInactive);
	connect(m_rHard, &QRadioButton::toggled, this, &Settings::makeLinesInactive);

	connect(m_rCustom, &QRadioButton::toggled, this, &Settings::makeLinesActive);

	connect(m_ok_btn, &QPushButton::clicked, this, &Settings::saveSettings);

	m_settings.setIniCodec("UTF-8");

	loadSettings();
}

void Settings::saveSettings()
{
	int w = saveGuard(m_width->text().toInt(), 50);
	int h = saveGuard(m_height->text().toInt(), 50);

	m_width->setText(QString::number(w));
	m_height->setText(QString::number(h));

	m_settings.setValue("width", m_width->text());
	m_settings.setValue("height", m_height->text());

	m_mines_amount->setText(QString::number(saveGuard(m_mines_amount->text().toInt(), w * h - 1)));
	m_settings.setValue("mines", m_mines_amount->text());

	m_settings.setValue("t_rEasy", QString::number(m_rEasy->isChecked()));
	m_settings.setValue("t_rMedium", QString::number(m_rMedium->isChecked()));
	m_settings.setValue("t_rHard", QString::number(m_rHard->isChecked()));
	m_settings.setValue("t_rCustom", QString::number(m_rCustom->isChecked()));

	m_settings.remove("cells");
	this->accept();
}

int Settings::saveGuard(int val, int max_val)
{
	if (val > max_val)
	{
		return max_val;
	}
	return val;
}

void Settings::loadSettings()
{
	QString p_width = m_settings.value("width", "10").toString();
	QString p_height = m_settings.value("height", "10").toString();
	QString p_mines = m_settings.value("mines", "10").toString();

	QString t_rEasy = m_settings.value("t_rEasy", "1").toString();
	QString t_rMedium = m_settings.value("t_rMedium", "0").toString();
	QString t_rHard = m_settings.value("t_rHard", "0").toString();
	QString t_rCustom = m_settings.value("t_rCustom", "0").toString();

	m_width->setText(settingsGuard(p_width, 2, "50"));
	m_height->setText(settingsGuard(p_height, 2, "50"));
	m_mines_amount->setText(settingsGuard(p_mines, 4, "2499"));

	m_rEasy->setChecked(btnGuard(t_rEasy));
	m_rMedium->setChecked(btnGuard(t_rMedium));
	m_rHard->setChecked(btnGuard(t_rHard));
	m_rCustom->setChecked(btnGuard(t_rCustom));

	int toggledBtns = 0;
	if (m_rEasy->isChecked())
	{
		toggledBtns++;
	}
	if (m_rMedium->isChecked())
	{
		toggledBtns++;
	}
	if (m_rHard->isChecked())
	{
		toggledBtns++;
	}
	if (m_rCustom->isChecked())
	{
		toggledBtns++;
	}

	if (toggledBtns != 1)
	{
		setDefaulBtnState();
	}
}

void Settings::setDefaulBtnState()
{
	m_rEasy->setChecked(1);
	m_rMedium->setChecked(0);
	m_rHard->setChecked(0);
	m_rCustom->setChecked(0);
}

bool Settings::btnGuard(QString state)
{
	if (state != "1")
	{
		return false;
	}
	return true;
}

QString Settings::settingsGuard(QString s_data, int max_size, QString max_value)
{
	if (s_data.size() > max_size)
	{
		return max_value;
	}

	if (s_data.size() == 0 || s_data.toInt() == 0)
	{
		return "10";
	}

	return s_data;
}

void Settings::initGUI()
{
	QGridLayout *propLines = new QGridLayout;
	propLines->addWidget(new QLabel(QApplication::translate("Game", "Height")), 0, 0);
	propLines->addWidget(new QLabel(QApplication::translate("Game", "Width")), 0, 1);
	propLines->addWidget(new QLabel(QApplication::translate("Game", "Mines")), 0, 2);
	propLines->addWidget(m_height = new QLineEdit, 1, 0);
	propLines->addWidget(m_width = new QLineEdit, 1, 1);
	propLines->addWidget(m_mines_amount = new QLineEdit, 1, 2);

	m_height->setInputMask("00");
	m_width->setInputMask("00");
	m_mines_amount->setInputMask("0000");

	QHBoxLayout *diffPressetBtnGroup = new QHBoxLayout;
	diffPressetBtnGroup->addWidget(m_rEasy = new QRadioButton(QApplication::translate("Game", "Easy")));
	diffPressetBtnGroup->addWidget(m_rMedium = new QRadioButton(QApplication::translate("Game", "Medium")));
	diffPressetBtnGroup->addWidget(m_rHard = new QRadioButton(QApplication::translate("Game", "Hard")));
	diffPressetBtnGroup->addWidget(m_rCustom = new QRadioButton(QApplication::translate("Game", "Custom")));

	QVBoxLayout *layout = new QVBoxLayout;
	layout->addLayout(propLines);
	layout->addLayout(diffPressetBtnGroup);
	layout->addWidget(m_ok_btn = new QPushButton("OK"));

	this->setWindowTitle(QApplication::translate("Game", "game"));
	this->setLayout(layout);
}

void Settings::setPreset()
{
	if (m_rEasy->isChecked())
	{
		m_height->setText(QString::number(m_easy_preset.height));
		m_width->setText(QString::number(m_easy_preset.width));
		m_mines_amount->setText(QString::number(m_easy_preset.mines_amount));
	}
	else if (m_rMedium->isChecked())
	{
		m_height->setText(QString::number(m_medium_preset.height));
		m_width->setText(QString::number(m_medium_preset.width));
		m_mines_amount->setText(QString::number(m_medium_preset.mines_amount));
	}
	else if (m_rHard->isChecked())
	{
		m_height->setText(QString::number(m_hard_preset.height));
		m_width->setText(QString::number(m_hard_preset.width));
		m_mines_amount->setText(QString::number(m_hard_preset.mines_amount));
	}
}

void Settings::makeLinesInactive()
{
	m_height->setReadOnly(true);
	m_width->setReadOnly(true);
	m_mines_amount->setReadOnly(true);

	m_height->setStyleSheet("background-color:grey;");
	m_width->setStyleSheet("background-color:grey;");
	m_mines_amount->setStyleSheet("background-color:grey;");
}

void Settings::makeLinesActive()
{
	m_height->setReadOnly(false);
	m_width->setReadOnly(false);
	m_mines_amount->setReadOnly(false);

	m_height->setStyleSheet("background-color:white;");
	m_width->setStyleSheet("background-color:white;");
	m_mines_amount->setStyleSheet("background-color:white;");
}
