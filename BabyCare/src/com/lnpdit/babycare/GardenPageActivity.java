package com.lnpdit.babycare;

import com.lnpdit.garden.GardenChuqinActivity;
import com.lnpdit.garden.GardenCourseActivity;
import com.lnpdit.garden.GardenFoodActivity;
import com.lnpdit.garden.GardenNewsActivity;
import com.lnpdit.garden.GardenPhotoActivity;
import com.lnpdit.garden.GardenTopicActivity;
import com.lnpdit.garden.GardenVideoActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class GardenPageActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardenpage);

		View garden_layout_news = findViewById(R.id.garden_layout_news);
		View garden_layout_photo = findViewById(R.id.garden_layout_photo);
		View garden_layout_video = findViewById(R.id.garden_layout_video);
		View garden_layout_topic = findViewById(R.id.garden_layout_topic);
		View garden_layout_food = findViewById(R.id.garden_layout_food);
		View garden_layout_course = findViewById(R.id.garden_layout_course);
		View garden_layout_chuqin = findViewById(R.id.garden_layout_chuqin);
		garden_layout_news.setOnClickListener(this);
		garden_layout_photo.setOnClickListener(this);
		garden_layout_video.setOnClickListener(this);
		garden_layout_topic.setOnClickListener(this);
		garden_layout_course.setOnClickListener(this);
		garden_layout_food.setOnClickListener(this);
		garden_layout_chuqin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.garden_layout_news:
			startActivity(new Intent(GardenPageActivity.this,
					GardenNewsActivity.class));
			break;
		case R.id.garden_layout_photo:
			startActivity(new Intent(GardenPageActivity.this,
					GardenPhotoActivity.class));
			break;
		case R.id.garden_layout_video:
			startActivity(new Intent(GardenPageActivity.this,
					GardenVideoActivity.class));
			break;
		case R.id.garden_layout_topic:
			startActivity(new Intent(GardenPageActivity.this,
					GardenTopicActivity.class));
			break;
		case R.id.garden_layout_food:
			startActivity(new Intent(GardenPageActivity.this,
					GardenFoodActivity.class));
			break;
		case R.id.garden_layout_course:
			startActivity(new Intent(GardenPageActivity.this,
					GardenCourseActivity.class));
			break;
		case R.id.garden_layout_chuqin:
			startActivity(new Intent(GardenPageActivity.this,
					GardenChuqinActivity.class));
			break;

		default:
			break;
		}
	}
}
