#include "congrats.h"

#include <QPushButton>
#include <QVBoxLayout>

Congrats::Congrats(QString msg, QWidget *parent) : QDialog(parent), m_btn(new QPushButton("OK", this))
{
	QVBoxLayout *layout = new QVBoxLayout;
	m_text = new QLabel(msg);
	layout->addWidget(m_text);
	layout->addWidget(m_btn);
	this->setLayout(layout);
	setWindowTitle(msg);
	connect(m_btn, &QPushButton::clicked, this, &Congrats::accept);

	this->setMinimumSize(70, 70);
	setSizePolicy(QSizePolicy::Minimum, QSizePolicy::Minimum);
}
