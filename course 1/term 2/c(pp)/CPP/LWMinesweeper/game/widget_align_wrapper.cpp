#include "widget_align_wrapper.h"

WidgetAlignWrapper::WidgetAlignWrapper(QWidget *item, QWidget *parent) : QWidget(parent), m_item(item)
{
	QHBoxLayout *layout = new QHBoxLayout;
	layout->addWidget(item);
	this->setLayout(layout);

	QSizePolicy sizePolicy(QSizePolicy::Expanding, QSizePolicy::Expanding);

	this->setSizePolicy(sizePolicy);
}

void WidgetAlignWrapper::set_widget(QWidget *new_item)
{
	m_item = new_item;
}
