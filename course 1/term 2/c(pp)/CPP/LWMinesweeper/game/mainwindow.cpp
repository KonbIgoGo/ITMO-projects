#include "mainwindow.h"

#include <QAction>
#include <QDebug>
#include <QHBoxLayout>
#include <QLabel>
#include <QMainWindow>
#include <QMenuBar>
#include <QSizePolicy>
#include <QStatusBar>
#include <QStyle>
#include <QToolBar>

MainWindow::MainWindow(bool dbg) : QMainWindow(), m_dbg(dbg)
{
	loadSettings();
	initGUI();
	this->setWindowTitle(QApplication::translate("Game", "Minesweeper"));

	connect(m_win_window, &Congrats::accepted, this, &MainWindow::restart);
	connect(m_defeat_window, &Congrats::accepted, this, &MainWindow::restart);
	connect(m_win_window, &Congrats::rejected, this, &MainWindow::restart);
	connect(m_defeat_window, &Congrats::rejected, this, &MainWindow::restart);

	connect(m_settings_btn, &QPushButton::clicked, m_settings_window, &Settings::exec);
	connect(m_settings_window, &Settings::accepted, this, &MainWindow::loadSettings);
}

void MainWindow::changeLanguage()
{
	m_win_window->deleteLater();
	m_defeat_window->deleteLater();

	this->setWindowTitle(QApplication::translate("Game", "Minesweeper"));
	m_win_window = new Congrats(QApplication::translate("Game", "YOU WIN"), this);
	m_defeat_window = new Congrats(QApplication::translate("Game", "YOU LOSE"), this);

	connect(m_win_window, &Congrats::accepted, this, &MainWindow::restart);
	connect(m_defeat_window, &Congrats::accepted, this, &MainWindow::restart);
	connect(m_win_window, &Congrats::rejected, this, &MainWindow::restart);
	connect(m_defeat_window, &Congrats::rejected, this, &MainWindow::restart);

	connect(m_field, &Field::onDefeat, m_defeat_window, &Congrats::exec);
	connect(m_field, &Field::onWin, m_win_window, &Congrats::exec);

	m_settings_window->deleteLater();
	m_settings_window = new Settings(this);
	connect(m_settings_btn, &QPushButton::clicked, m_settings_window, &Settings::exec);
	connect(m_settings_window, &Settings::accepted, this, &MainWindow::loadSettings);
}

void MainWindow::changeEvent(QEvent *e)
{
	if (e->type() == QEvent::LanguageChange)
	{
		changeLanguage();
	}
}

void MainWindow::resizeEvent(QResizeEvent *e)
{
	int side = qMin(e->size().height(), e->size().width());

	this->m_field->setMaximumSize(side, side + 50);

	QMainWindow::resizeEvent(e);
};

void MainWindow::loadSettings()
{
	QString width = m_settings.value("width", "10").toString();
	QString height = m_settings.value("height", "10").toString();
	QString mines = m_settings.value("mines", "10").toString();

	m_field_data.width = settingsGuard(width, 4, m_MAX_S);
	m_field_data.height = settingsGuard(height, 4, m_MAX_S);
	m_field_data.mines_amount = settingsGuard(mines, 6, m_field_data.height * m_field_data.width - 1);
	restart();
}

unsigned int MainWindow::settingsGuard(QString s_data, int max_size, unsigned int max_value)
{
	if (s_data.size() > max_size || s_data.toUInt() > max_value)
	{
		return max_value;
	}

	if (s_data.size() == 0 || s_data.toInt() <= 0)
	{
		return 10;
	}

	return s_data.toUInt();
}

void MainWindow::initGUI()
{
	setWindowTitle("Minesweeper");

	m_tools = new QToolBar();
	m_tools->setFixedHeight(50);

	m_settings_btn = new QPushButton();
	m_language_list = new QComboBox();

	m_language_list->addItems(
		QStringList()
		<< "ru_RU"
		<< "en_US"
		<< "ar_TD");

	connect(m_language_list,
			QOverload< const QString & >::of(&QComboBox::currentIndexChanged),
			[=](const QString &loc)
			{
				const QString baseName = "game_" + QLocale(loc).name();
				m_translator.load(":/i18n/" + baseName);
				qApp->installTranslator(&m_translator);
			});

	m_settings_btn->setIcon(QIcon(":/icons/setting.svg"));
	m_settings_btn->setFixedHeight(50);

	m_tools->addWidget(m_settings_btn);
	m_tools->addWidget(m_language_list);
	this->addToolBar(m_tools);

	QSizePolicy sizePolicy(QSizePolicy::Preferred, QSizePolicy::Preferred);

	this->setSizePolicy(sizePolicy);
}

void MainWindow::restart()
{
	if (m_field != nullptr)
	{
		m_field->deleteLater();
		m_central_widget->deleteLater();
		m_settings.remove("cells");
	}
	m_field = new Field(m_field_data, m_dbg);
	m_central_widget = new WidgetAlignWrapper(m_field, this);
	setCentralWidget(m_central_widget);

	connect(m_field, &Field::onSettingsErr, this, &MainWindow::restart);
	connect(m_field, &Field::onDefeat, m_defeat_window, &Congrats::exec);
	connect(m_field, &Field::onWin, m_win_window, &Congrats::exec);

	m_field->loadSettings();
}

MainWindow::~MainWindow() {}
