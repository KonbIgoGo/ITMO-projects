#include "field.h"

#include <QtWidgets>
Field::Field(specs data, bool dbg, QWidget *parent) : QWidget(parent), m_dbg(dbg)
{
	initGUI();
	initGame(data);
	this->setMinimumSize(QSize(25 * m_width, 25 * m_height + 70));
}

void Field::resizeEvent(QResizeEvent *e)
{
	int side = qMin((e->size().height() - 70) / m_height, (e->size().width()) / m_width);
	QSize cell_size(side, side);
	for (unsigned int i = 0; i < m_height; i++)
	{
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i][j]->setMaximumSize(cell_size);
		}
	}
	this->setMaximumWidth(side * m_width);
	QWidget::resizeEvent(e);
}

void Field::loadSettings()
{
	if (m_settings.childGroups().contains("cells"))
	{
		m_settings.beginGroup("cells");
		for (unsigned int i = 0; i < m_height; i++)
		{
			for (unsigned int j = 0; j < m_width; j++)
			{
				QString cordGroup = QString::number(i);
				cordGroup.append(" ");
				cordGroup.append(QString::number(j));
				m_settings.beginGroup(cordGroup);

				if (propGuard(m_settings.value("fClicked", "0").toString()))
				{
					m_settings_clicks++;

					m_cells[i][j]->setFClick();
				}

				if (propGuard(m_settings.value("mined", "0").toString()))
				{
					m_settings_mines++;
					m_cells[i][j]->setupMine();
				}

				if (propGuard(m_settings.value("flagged", "0").toString()))
				{
					m_cells[i][j]->toggleFlag();
				}

				if (propGuard(m_settings.value("questioned", "0").toString()))
				{
					m_cells[i][j]->toggleFlag();
					m_cells[i][j]->toggleFlag();
				}
				if (!propGuard(m_settings.value("active", "1").toString()))
				{
					m_cells[i][j]->setActive(false);
				}
				m_settings.endGroup();
			}
		}

		if (m_settings_clicks != 0 && (m_settings_clicks != m_width * m_height || m_settings_mines != m_mined))
		{
			settingsErrHandler();
		}
		defineNeighbours();

		for (unsigned int i = 0; i < m_height; i++)
		{
			for (unsigned int j = 0; j < m_width; j++)
			{
				QString cordGroup = QString::number(i);
				cordGroup.append(" ");
				cordGroup.append(QString::number(j));
				m_settings.beginGroup(cordGroup);
				if (propGuard(m_settings.value("released", "0").toString()))
				{
					if (!m_cells[i][j]->isMined())
					{
						m_cells[i][j]->release();
					}
					else
					{
						settingsErrHandler();
					}
				}
				m_settings.endGroup();
			}
		}
		m_settings.endGroup();
	}
}

bool Field::propGuard(QString value)
{
	QRegExp checker_T("1");
	QRegExp checker_F("0");
	if (checker_T.exactMatch(value))
	{
		return true;
	}
	else if (checker_F.exactMatch(value))
	{
		return false;
	}
	else
	{
		settingsErrHandler();
	}
	return false;
}

void Field::initGUI()
{
	QGridLayout *main_layout = new QGridLayout;
	this->setSizePolicy(QSizePolicy::Minimum, QSizePolicy::Minimum);

	main_layout->setMargin(0);
	main_layout->setSpacing(0);
	main_layout->setContentsMargins(0, 0, 0, 0);

	QHBoxLayout *tools_layout = new QHBoxLayout;

	tools_layout->setMargin(0);
	tools_layout->setSpacing(0);
	tools_layout->setContentsMargins(0, 0, 0, 0);
	m_counter = new Counter(this);
	m_control_swap_btn = new QPushButton(this);

	m_control_swap_btn->setFixedSize(70, 70);

	m_control_swap_btn->setIcon(QIcon(":/icons/left_hand_off.svg"));
	m_control_swap_btn->setIconSize(QSize(70, 70));

	connect(m_control_swap_btn, &QPushButton::clicked, this, &Field::leftHandToggle);

	tools_layout->addWidget(m_control_swap_btn, 0, Qt::AlignLeft);
	tools_layout->addWidget(m_counter, 0, Qt::AlignHCenter);

	if (m_dbg)
	{
		m_dbg_btn = new QPushButton(this);
		m_dbg_btn->setFixedSize(70, 70);
		m_dbg_btn->setIcon(QIcon(":/icons/dbg_off.svg"));
		m_dbg_btn->setIconSize(QSize(70, 70));

		connect(m_dbg_btn, &QPushButton::clicked, this, &Field::dbgHandler);
		tools_layout->addWidget(m_dbg_btn, 0, Qt::AlignRight);
	}

	QVBoxLayout *layout = new QVBoxLayout;
	layout->addLayout(tools_layout);
	layout->addLayout(main_layout);

	layout->setMargin(0);
	layout->setSpacing(0);
	layout->setContentsMargins(0, 0, 0, 0);
	this->setLayout(layout);
}

void Field::initGame(specs data)
{
	m_height = data.height;
	m_width = data.width;
	m_mined = data.mines_amount;
	m_counter->set_mines(m_mined);

	for (unsigned int i = 0; i < m_height; i++)
	{
		m_cells += QList< Cell * >{};
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i] += new Cell({ i, j }, this);
			connect(m_cells[i][j], &Cell::firstClicked, this, &Field::firstClick);
			connect(m_cells[i][j], &Cell::isFlagged, this, &Field::flagged);
			connect(m_cells[i][j], &Cell::isQuestioned, this, &Field::questioned);
			connect(m_cells[i][j], &Cell::onReleased, this, &Field::releaseCount);
			connect(m_cells[i][j], &Cell::onDefeat, this, &Field::boomHandler);
			static_cast< QGridLayout * >(this->layout()->children()[1])->addWidget(m_cells[i][j], i, j);
		}
	}
}

void Field::showMines()
{
	for (unsigned int i = 0; i < m_height; i++)
	{
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i][j]->toggleDbg();
		}
	}
}

void Field::dbgHandler()
{
	m_dbg_show = !m_dbg_show;
	if (m_dbg_show)
	{
		m_dbg_btn->setIcon(QIcon(":/icons/dbg_on.svg"));
		m_dbg_btn->setIconSize(QSize(70, 70));
	}
	else
	{
		m_dbg_btn->setIcon(QIcon(":/icons/dbg_off.svg"));
		m_dbg_btn->setIconSize(QSize(70, 70));
	}
	showMines();
}

void Field::settingsErrHandler()
{
	m_settings.remove("cells");
	emit onSettingsErr();
}

void Field::propGuardMines()
{
	m_settings_mines++;
}

void Field::propGuardClick()
{
	m_settings_clicks++;
}

void Field::reverseControl()
{
	for (unsigned int i = 0; i < m_height; i++)
	{
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i][j]->reverseControl();
		}
	}
}

void Field::leftHandToggle()
{
	m_control_swapped = !m_control_swapped;
	if (m_control_swapped)
	{
		m_control_swap_btn->setIcon(QIcon(":/icons/left_hand_on.svg"));
		m_control_swap_btn->setIconSize(QSize(70, 70));
	}
	else
	{
		m_control_swap_btn->setIcon(QIcon(":/icons/left_hand_off.svg"));
		m_control_swap_btn->setIconSize(QSize(70, 70));
	}
	reverseControl();
}

void Field::questioned()
{
	m_flag_count--;
	m_counter->set_flags(m_flag_count);
}

void Field::flagged()
{
	m_flag_count++;
	m_counter->set_flags(m_flag_count);
}

void Field::makeInactive()
{
	for (unsigned int i = 0; i < m_height; i++)
	{
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i][j]->setEnabled(false);
		}
	}
}

void Field::releaseCount()
{
	m_released++;
	if (m_released == m_width * m_height - m_mined)
	{
		makeInactive();
		showMines();
		emit onWin();
	}
}

void Field::boomHandler(const position cords)
{
	makeInactive();
	showMines();
	m_cells[cords.col][cords.row]->setDefeatStyle();
	emit onDefeat();
}

void Field::defineNeighbours()
{
	for (unsigned int i = 0; i < m_height; i++)
	{
		for (unsigned int j = 0; j < m_width; j++)
		{
			m_cells[i][j]->setFirstClicked();

			if (i != 0)
			{
				m_cells[i][j]->addNeighbour(m_cells[i - 1][j]);
				if (j != m_width - 1)
				{
					m_cells[i][j]->addNeighbour(m_cells[i - 1][j + 1]);
				}
				if (j != 0)
				{
					m_cells[i][j]->addNeighbour(m_cells[i - 1][j - 1]);
				}
			}
			if (i != m_height - 1)
			{
				m_cells[i][j]->addNeighbour(m_cells[i + 1][j]);
				if (j != m_width - 1)
				{
					m_cells[i][j]->addNeighbour(m_cells[i + 1][j + 1]);
				}
				if (j != 0)
				{
					m_cells[i][j]->addNeighbour(m_cells[i + 1][j - 1]);
				}
			}
			if (j != 0)
			{
				m_cells[i][j]->addNeighbour(m_cells[i][j - 1]);
			}
			if (j != m_width - 1)
			{
				m_cells[i][j]->addNeighbour(m_cells[i][j + 1]);
			}
		}
	}
}

void Field::firstClick(position cord)
{
	unsigned int rh, rw;
	if (m_height != 0 && m_width != 0)
	{
		for (unsigned int i = 0; i < m_mined; i++)
		{
			if (m_height != 0 && m_width != 0)
			{
				rh = qrand() % m_height;
				rw = qrand() % m_width;
				while (m_cells[rh][rw]->isMined() || ((rh == cord.col) && (rw == cord.row)))
				{
					rh = qrand() % m_height;
					rw = qrand() % m_width;
				}

				m_cells[rh][rw]->setupMine();
			}
		}
	}
	defineNeighbours();
	m_cells[cord.col][cord.row]->release();
}
