package com.zoeetrope.lineupcamera;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.zoeetrope.lineupcamera.model.Album;

public class AlbumListActivity extends ListActivity {

	static final int DIALOG_NEW_ALBUM_ID = 0;
	static final int DIALOG_RENAME_ALBUM_ID = 1;

	private ArrayList<Album> mAlbums;
	private Button mNewAlbumButton;
	private AlbumListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.album_list);
		loadAlbums();

		registerForContextMenu(getListView());

		mNewAlbumButton = (Button) findViewById(R.id.newAlbum);
		mNewAlbumButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlbumListActivity.this.showDialog(DIALOG_NEW_ALBUM_ID);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		AlertDialog.Builder builder;
		AlertDialog alertDialog = null;
		Context context = AlbumListActivity.this;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.album_name_dialog, null);

		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		builder.setTitle(R.string.ablum_name_dialog_title);

		switch (id) {
		case DIALOG_NEW_ALBUM_ID:
			builder.setPositiveButton(R.string.ok_button,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent cameraIntent = new Intent();
							cameraIntent.setComponent(new ComponentName(
									AlbumListActivity.this,
									LineUpCameraActivity.class));

							EditText name = (EditText) layout
									.findViewById(R.id.albumName);

							Bundle bundle = new Bundle();
							bundle.putString("ALBUM", name.getText().toString());
							cameraIntent.putExtras(bundle);

							AlbumListActivity.this.startActivity(cameraIntent);
						}
					});
			break;
		case DIALOG_RENAME_ALBUM_ID:
			final int albumIndex = bundle.getInt("ALBUM_INDEX");

			builder.setPositiveButton(R.string.ok_button,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText name = (EditText) layout
									.findViewById(R.id.albumName);
							Album album = mAlbums.get(albumIndex);

							album.renameAlbum(name.getText().toString());
						}
					});
			break;
		}

		alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		return alertDialog;
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadAlbums();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent imageListIntent = new Intent();
		imageListIntent.setComponent(new ComponentName(AlbumListActivity.this,
				ImageListActivity.class));

		Bundle bundle = new Bundle();
		bundle.putString("ALBUM", mAlbums.get(position).getName());
		imageListIntent.putExtras(bundle);

		AlbumListActivity.this.startActivity(imageListIntent);
	}

	private void loadAlbums() {
		mAlbums = new ArrayList<Album>();
		File storageFolder = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"LineUpCamera");

		if (!storageFolder.exists()) {
			if (!storageFolder.mkdirs()) {
				Log.d("LineUpCamera", "failed to create directory");
			}
		}

		for (File child : storageFolder.listFiles()) {
			if (child.isDirectory()) {
				mAlbums.add(new Album(child.getName()));
			}
		}

		mAdapter = new AlbumListAdapter(this, R.layout.album_list_item, mAlbums);
		setListAdapter(mAdapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.album_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.remove:
			mAlbums.get(info.position).removeAlbum();
			mAlbums.remove(info.position);

			mAdapter.notifyDataSetChanged();
			getListView().invalidateViews();
			return true;
		case R.id.rename:
			Bundle bundle = new Bundle();
			bundle.putInt("ALBUM_INDEX", info.position);

			AlbumListActivity.this.showDialog(DIALOG_RENAME_ALBUM_ID, bundle);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
