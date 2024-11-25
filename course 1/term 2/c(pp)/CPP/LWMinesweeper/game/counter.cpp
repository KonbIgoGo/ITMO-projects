#include "counter.h"

#include <QHBoxLayout>

Counter::Counter(QWidget *parent) : QLCDNumber(parent)
{
	this->setDigitCount(6);
	this->display(0);
	this->setSegmentStyle(QLCDNumber::Flat);
	this->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Fixed);
	this->setMaximumWidth(350);
	this->setStyleSheet(m_style);
}

void Counter::set_mines(int m_amount)
{
	m_mines = m_amount;
	this->display(m_mines);
}

void Counter::set_flags(int f_amount)
{
	m_flags = f_amount;
	this->display(m_mines - m_flags);
}

QSize Counter::sizeHint() const
{
	return QSize(200, 70);
}
