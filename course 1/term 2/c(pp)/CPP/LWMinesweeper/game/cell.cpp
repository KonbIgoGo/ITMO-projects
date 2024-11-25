#include "cell.h"

#include <QLayout>
#include <QSizePolicy>
#include <QStyle>

Cell::Cell(position cords, QWidget *parent) : QPushButton(parent), cord(cords)
{
	QSizePolicy sizePolicy(QSizePolicy::Minimum, QSizePolicy::Minimum);
	sizePolicy.setHeightForWidth(true);
	this->setSizePolicy(sizePolicy);
	this->setStyleSheet(m_unreleased_style);
	connect(this, &Cell::clicked, this, &Cell::release);
	connect(this, &Cell::clicked, this, &Cell::firstClick);
	connect(this, &Cell::rightClicked, this, &Cell::toggleFlag);
}

void Cell::setDefeatStyle()
{
	this->setStyleSheet(m_boom_style);
}

int Cell::heightForWidth(int w) const
{
	return w;
}

void Cell::toggleDbg()
{
	dbg = !dbg;
	if (dbg && mined)
	{
		this->setStyleSheet(m_bomb_debug_style);
	}
	else if (!dbg && mined)
	{
		if (questioned)
		{
			this->setStyleSheet(m_questioned_style);
		}
		else if (flagged)
		{
			this->setStyleSheet(m_flagged_style);
		}
		else
		{
			this->setStyleSheet(m_unreleased_style);
		}
	}
}

void Cell::setFClick()
{
	fClicked = true;
}

bool Cell::isFirstClicked()
{
	return fClicked;
}

void Cell::saveSettings(QString k, bool v)
{
	settings.beginGroup("cells");
	QString cordGroup = QString::number(cord.col);
	cordGroup.append(" ");
	cordGroup.append(QString::number(cord.row));
	settings.beginGroup(cordGroup);

	settings.setValue(k, QString::number(v));

	settings.endGroup();
	settings.endGroup();
}
void Cell::reverseControl()
{
	controlReverse = !controlReverse;
}

void Cell::midClickHandler()
{
	for (QSet< Cell * >::iterator it = m_neighbours.begin(); it != m_neighbours.end(); it++)
	{
		(*it)->release();
	}
}

QSize Cell::sizeHint() const
{
	QSize size = QPushButton::sizeHint();
	int side = qMin(size.width(), size.height());
	return QSize(side, side);
}

void Cell::setActive(bool val)
{
	active = val;
}

void Cell::mousePressEvent(QMouseEvent *e)
{
	if (e->button() == Qt::RightButton)
	{
		if (controlReverse)
		{
			if (active)
			{
                emit clicked();
			}
		}
		else
		{
			emit rightClicked();
		}
	}
	else if (e->button() == Qt::MiddleButton)
	{
		midClickHandler();
	}
	else if (e->button() == Qt::LeftButton)
	{
		if (controlReverse)
		{
			emit rightClicked();
		}
		else
		{
			if (active)
			{
				emit clicked();
			}
		}
	}
}

void Cell::toggleFlag()
{
	if (!released)
	{
		if (flagged)
		{
			flagged = false;
			questioned = true;
		}
		else if (questioned)
		{
			flagged = false;
			questioned = false;
		}
		else
		{
			flagged = true;
		}
		if (flagged)
		{
			this->setActive(false);
			this->setStyleSheet(m_flagged_style);
			emit isFlagged();
		}
		else if (questioned)
		{
			this->setActive(false);
			this->setStyleSheet(m_questioned_style);
			emit isQuestioned();
		}
		else
		{
			this->setActive(true);
			if (mined && dbg)
			{
				this->setStyleSheet(m_bomb_debug_style);
			}
			else
			{
				this->setStyleSheet(m_unreleased_style);
			}
		}

		saveSettings("flagged", flagged);
		saveSettings("questioned", questioned);
		saveSettings("active", active);
	}
}

void Cell::setFirstClicked()
{
	fClicked = true;
	saveSettings("fClicked", fClicked);
}

void Cell::addNeighbour(Cell *item)
{
	m_neighbours.insert(item);
	if (item->isMined())
	{
		m_mined_neighbours.insert(item);
	}
}

void Cell::setupMine()
{
	if (dbg)
	{
		this->setStyleSheet(m_bomb_debug_style);
	}
	mined = true;
	saveSettings("mined", mined);
}

bool Cell::isMined()
{
	return mined;
}

bool Cell::isReleased()
{
	return released;
}

void Cell::firstClick()
{
	if (!fClicked)
	{
		emit firstClicked(cord);
	}
}

void Cell::release()
{
	if (!released && fClicked && !flagged && !questioned)
	{
		released = true;

		if (!this->isMined())
		{
			this->setStyleSheet(m_released_style);
			if (m_mined_neighbours.size() == 0)
			{
				for (QSet< Cell * >::iterator i = m_neighbours.begin(); i != m_neighbours.end(); i++)
				{
					(*i)->release();
				}
			}
			else
			{
				int num = m_mined_neighbours.size();
				QString color;
				switch (num)
				{
				case 1:
					color = "blue";
					break;
				case 2:
					color = "green";
					break;
				case 3:
					color = "red";
					break;
				case 4:
					color = "darkblue";
					break;
				case 5:
					color = "brown";
					break;
				case 6:
					color = "cyan";
					break;
				case 7:
					color = "black";
					break;
				case 8:
					color = "gray";
					break;
				}

				this->setText(QString::number(num));
				this->setStyleSheet(m_numbered_style.arg(color));
			}
		}
		saveSettings("released", released);
		saveSettings("active", active);
		if (!mined)
		{
			emit onReleased();
		}
		else
		{
			emit onDefeat(cord);
		}

		this->setActive(false);
	}
}
